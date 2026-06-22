# =========================================================
# NPC Translator - Multi-Version Build Script
# Her çağrıda 3 Minecraft sürümü için derleme yapar ve
# C:\Users\burha\Desktop\npc-mods klasörüne kopyalar.
# =========================================================

$JAVA21 = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
$JAVA26 = "$PSScriptRoot\oracleJdk-26"
$OutputDir = "C:\Users\burha\Desktop\npc-mods"
$ScriptDir = $PSScriptRoot
$WrapperProps = "$ScriptDir\gradle\wrapper\gradle-wrapper.properties"
$BuildGradle = "$ScriptDir\build.gradle"
$SettingsGradle = "$ScriptDir\settings.gradle"

$GRADLE_941 = "https\://services.gradle.org/distributions/gradle-9.4.1-bin.zip"
$GRADLE_960 = "https\://services.gradle.org/distributions/gradle-9.6.0-bin.zip"

# 26.x settings.gradle - pluginManagement olmadan (buildscript ile yükleniyor)
$Settings26x = @"
// Should match your modid
rootProject.name = 'modid'
"@

# Çıktı klasörünü oluştur
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

# Sürüm tanımları
$Versions = @(
    @{ Name="1.21.11"; Props="gradle.properties";      Suffix="mc1.21.11"; JavaHome=$JAVA21; BuildFile=$null;               GradleUrl=$GRADLE_941; Is26x=$false },
    @{ Name="26.2";    Props="gradle-26.2.properties"; Suffix="mc26.2";    JavaHome=$JAVA26; BuildFile="build-26x.gradle"; GradleUrl=$GRADLE_960; Is26x=$true  },
    @{ Name="26.1";    Props="gradle-26.1.properties"; Suffix="mc26.1";    JavaHome=$JAVA26; BuildFile="build-26x.gradle"; GradleUrl=$GRADLE_960; Is26x=$true  }
)

$Success = @()
$Failed  = @()

# Orijinal dosya içeriklerini yedekle
$OriginalWrapperContent = Get-Content $WrapperProps -Raw
$OriginalGradleProps = Get-Content "$ScriptDir\gradle.properties" -Raw
$OriginalBuildGradle = Get-Content $BuildGradle -Raw
$OriginalSettingsGradle = Get-Content $SettingsGradle -Raw

foreach ($v in $Versions) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host " Building for Minecraft $($v.Name)..." -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    # JAVA_HOME ayarla
    $env:JAVA_HOME = $v.JavaHome

    # Gradle wrapper sürümünü ayarla
    $wrapperContent = $OriginalWrapperContent -replace 'distributionUrl=.*', "distributionUrl=$($v.GradleUrl)"
    Set-Content -Path $WrapperProps -Value $wrapperContent -NoNewline

    # gradle.properties'i ayarla
    if ($v.Props -ne "gradle.properties") {
        Copy-Item "$ScriptDir\$($v.Props)" "$ScriptDir\gradle.properties" -Force
    }

    # build.gradle ve settings.gradle'ı ayarla (26.x için)
    if ($v.Is26x) {
        Copy-Item "$ScriptDir\$($v.BuildFile)" $BuildGradle -Force
        Set-Content -Path $SettingsGradle -Value $Settings26x
    }

    # Derle
    & "$ScriptDir\gradlew.bat" clean build 2>&1 | Tee-Object -Variable buildOutput | Out-Null
    $ExitCode = $LASTEXITCODE

    # Orijinal dosyaları geri yükle
    Set-Content -Path "$ScriptDir\gradle.properties" -Value $OriginalGradleProps -NoNewline
    Set-Content -Path $BuildGradle -Value $OriginalBuildGradle -NoNewline
    Set-Content -Path $SettingsGradle -Value $OriginalSettingsGradle -NoNewline
    Set-Content -Path $WrapperProps -Value $OriginalWrapperContent -NoNewline

    if ($ExitCode -eq 0) {
        $JarFiles = Get-ChildItem "$ScriptDir\build\libs\npc_translator-*.jar" | Where-Object { $_.Name -notmatch "sources" }
        if ($JarFiles.Count -gt 0) {
            $SrcJar = $JarFiles[0].FullName
            $DestJar = "$OutputDir\npc_translator-1.3.0-$($v.Suffix).jar"
            Copy-Item $SrcJar $DestJar -Force
            Write-Host " SUCCESS -> $DestJar" -ForegroundColor Green
            $Success += $v.Name
        } else {
            Write-Host " WARNING: Build OK but JAR not found!" -ForegroundColor Yellow
            $Failed += $v.Name
        }
    } else {
        Write-Host " FAILED (Exit: $ExitCode)" -ForegroundColor Red
        $Failed += $v.Name
        $buildOutput | Out-File -FilePath "$ScriptDir\build_error_$($v.Name).log" -Encoding utf8
        $buildOutput | Select-Object -Last 20 | ForEach-Object { Write-Host $_ }
    }
}

# Son olarak her şeyi geri yükle
Set-Content -Path $WrapperProps -Value $OriginalWrapperContent -NoNewline
Set-Content -Path "$ScriptDir\gradle.properties" -Value $OriginalGradleProps -NoNewline
Set-Content -Path $BuildGradle -Value $OriginalBuildGradle -NoNewline
Set-Content -Path $SettingsGradle -Value $OriginalSettingsGradle -NoNewline
$env:JAVA_HOME = $JAVA21

Write-Host ""
Write-Host "========================================" -ForegroundColor White
Write-Host " OZET / SUMMARY" -ForegroundColor White
Write-Host "========================================" -ForegroundColor White
if ($Success.Count -gt 0) {
    Write-Host " SUCCESS: $($Success -join ', ')" -ForegroundColor Green
}
if ($Failed.Count -gt 0) {
    Write-Host " FAILED:  $($Failed -join ', ')" -ForegroundColor Red
}
Write-Host ""
Write-Host " Output: $OutputDir" -ForegroundColor Cyan
Write-Host ""
