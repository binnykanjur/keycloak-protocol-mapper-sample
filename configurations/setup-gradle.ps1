function Add-PathVariable {
    param (
        [string]$PathToAdd
    )

    $currentPath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)
    if ($currentPath -notlike "*$PathToAdd*") {
        $newPath = "$currentPath;$PathToAdd"
        [System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::Machine)
    }
}

# Install Gradle
$gradleVersion = '8.8'
Write-Host "Installing Gradle ($gradleVersion)..." -ForegroundColor Yellow

$gradleUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
$gradleZipPath = "$env:TEMP\gradle-$gradleVersion-bin.zip"
$gradleExtractPath = "C:\Gradle"

Invoke-WebRequest -Uri $gradleUrl -OutFile $gradleZipPath
Expand-Archive -Path $gradleZipPath -DestinationPath $gradleExtractPath

$gradleHome = "$gradleExtractPath\gradle-$gradleVersion"
[System.Environment]::SetEnvironmentVariable("GRADLE_HOME", $gradleHome, [System.EnvironmentVariableTarget]::Machine)
Add-PathVariable -PathToAdd "$gradleHome\bin"

$envGradleHome = [System.Environment]::GetEnvironmentVariable("GRADLE_HOME", [System.EnvironmentVariableTarget]::Machine)
Write-Output "GRADLE_HOME is set to: $envGradleHome"
Write-Host "Gradle ($gradleVersion) installation completed successfully!" -ForegroundColor Green