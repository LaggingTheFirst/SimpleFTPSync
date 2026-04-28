# Windows SFTP Setup

This is the easiest Windows setup for SimpleFTPSync.

Use a shared folder at C:\MinecraftSync and point SFTP there.

1. Install OpenSSH Server from Windows Optional Features.
2. Start the SSH service as administrator with: net start sshd
3. Set it to automatic with: Set-Service -Name sshd -StartupType Automatic
4. Create an SFTP user.
   Use the net user command to create mcbackup with password wayback673.
5. Create the folder C:\MinecraftSync.
6. Grant full access to mcbackup and your normal Windows account on C:\MinecraftSync.
7. Test SFTP locally by connecting to 127.0.0.1 with the mcbackup account.
8. In the SFTP session, change into /C:/MinecraftSync and confirm it opens.

Recommended plugin config:
sync-type: SFTP
sftp.server: 127.0.0.1
sftp.port: 22
sftp.username: mcbackup
sftp.password: wayback673
sftp.remote-path: /C:/MinecraftSync
sync-folders[0].local-path: world
sync-folders[0].remote-path: world
sync-folders[0].exclude: **/session.lock

With this setup, the world folder uploads into C:\MinecraftSync\world.

Final test: run /sfs reload, then /sfs run, then check C:\MinecraftSync\world.
