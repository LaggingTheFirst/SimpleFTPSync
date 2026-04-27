$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$spigotRepo = Join-Path $env:USERPROFILE ".m2\repository\org\spigotmc\spigot-api"
$originalJar = Join-Path $projectRoot "SimpleFTPSync-1.1.jar"
$extractedDir = Join-Path $projectRoot "extracted"
$buildDir = Join-Path $projectRoot "build"
$classesDir = Join-Path $buildDir "classes"
$stageDir = Join-Path $buildDir "stage"
$outputJar = Join-Path $buildDir "SimpleFTPSync-1.1-recovered.jar"
$javac = "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\javac.exe"
$jarTool = "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\jar.exe"

if (-not (Test-Path $originalJar)) {
    throw "Missing original plugin jar at $originalJar"
}

if (-not (Test-Path $extractedDir)) {
    throw "Missing extracted jar contents at $extractedDir"
}

$spigotJar = Get-ChildItem $spigotRepo -Recurse -Filter "spigot-api-*-SNAPSHOT.jar" -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $spigotJar) {
    throw "Could not find a cached Spigot API snapshot jar under $spigotRepo"
}

Remove-Item -Recurse -Force $buildDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $classesDir | Out-Null
New-Item -ItemType Directory -Force $stageDir | Out-Null

$pomVersion = ([xml](Get-Content (Join-Path $projectRoot "pom.xml"))).project.version
$classpath = "$spigotJar;$originalJar"
$sources = Get-ChildItem (Join-Path $projectRoot "src\main\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

& $javac --release 8 -cp $classpath -d $classesDir $sources

Copy-Item -Recurse -Force (Join-Path $projectRoot "extracted\*") $stageDir
Copy-Item -Recurse -Force (Join-Path $classesDir "org") $stageDir
$pluginYml = Get-Content (Join-Path $projectRoot "src\main\resources\plugin.yml") -Raw
$pluginYml = $pluginYml.Replace('${project.version}', $pomVersion)
Set-Content -Path (Join-Path $stageDir "plugin.yml") -Value $pluginYml -NoNewline
Copy-Item -Force (Join-Path $projectRoot "src\main\resources\config.yml") (Join-Path $stageDir "config.yml")

& $jarTool cfm $outputJar (Join-Path $stageDir "META-INF\MANIFEST.MF") -C $stageDir .

Write-Host "Built $outputJar"
