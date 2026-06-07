# =============================================================================
# Utility Billing System — local run (Cursor / PowerShell terminal)
# =============================================================================
# Directory: run this from the project root (folder that contains pom.xml and mvnw.cmd)
#   c:\Users\hirwa\ubs
#
# Prerequisites:
#   - PostgreSQL installed and its Windows service running (port 5432)
#   - On first run only, postgres superuser password:
#       $env:POSTGRES_PASSWORD = "password-you-set-during-postgresql-install"
#
# What happens automatically:
#   1. run-local.ps1 creates ubs_user + ubs_db if they do not exist yet
#   2. Spring Boot / Hibernate creates and updates all tables (ddl-auto=update)
#   3. Demo users and sample data are inserted on first empty database (app.seed.enabled)
#
# Before each terminal session, set your Gmail app password:
#   $env:MAIL_PASSWORD = "your-16-char-app-password"
#
# Stop app: Ctrl+C in this terminal
# Connect to DB: psql -U ubs_user -d ubs_db -h localhost
# =============================================================================

Set-Location $PSScriptRoot

if (-not $env:MAIL_PASSWORD) {
    Write-Host "MAIL_PASSWORD is not set. Run this first:" -ForegroundColor Red
    Write-Host '  $env:MAIL_PASSWORD = "your-gmail-app-password"' -ForegroundColor Red
    exit 1
}

$pgReady = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue
if (-not $pgReady.TcpTestSucceeded) {
    Write-Host "PostgreSQL is not reachable on localhost:5432." -ForegroundColor Red
    Write-Host "Install PostgreSQL and start its Windows service, then run this script again." -ForegroundColor Yellow
    exit 1
}

function Find-Psql {
    $cmd = Get-Command psql -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $versions = 17, 16, 15, 14
    foreach ($v in $versions) {
        $path = "C:\Program Files\PostgreSQL\$v\bin\psql.exe"
        if (Test-Path $path) { return $path }
    }
    return $null
}

function Test-UbsDatabase {
    param([string]$Psql)
    $prev = $env:PGPASSWORD
    $env:PGPASSWORD = "ubs_password"
    & $Psql -U ubs_user -d ubs_db -h localhost -tAc "SELECT 1" 2>$null | Out-Null
    $ok = ($LASTEXITCODE -eq 0)
    $env:PGPASSWORD = $prev
    return $ok
}

function Initialize-UbsDatabase {
    param([string]$Psql)

    if (Test-UbsDatabase -Psql $Psql) {
        return $true
    }

    if (-not $env:POSTGRES_PASSWORD) {
        Write-Host "First run: database ubs_db is not set up yet." -ForegroundColor Yellow
        Write-Host "Set the postgres superuser password from your PostgreSQL install:" -ForegroundColor Yellow
        Write-Host '  $env:POSTGRES_PASSWORD = "your-postgres-install-password"' -ForegroundColor Yellow
        Write-Host "Then run .\run-local.ps1 again." -ForegroundColor Yellow
        return $false
    }

    Write-Host "Creating PostgreSQL user and database (first run only)..." -ForegroundColor Cyan
    $prev = $env:PGPASSWORD
    $env:PGPASSWORD = $env:POSTGRES_PASSWORD

    & $Psql -U postgres -h localhost -f "scripts\setup-database.sql" 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Could not create ubs_user. Check POSTGRES_PASSWORD." -ForegroundColor Red
        $env:PGPASSWORD = $prev
        return $false
    }

    $dbExists = & $Psql -U postgres -h localhost -tAc "SELECT 1 FROM pg_database WHERE datname = 'ubs_db'"
    if (-not $dbExists) {
        & $Psql -U postgres -h localhost -c "CREATE DATABASE ubs_db OWNER ubs_user" 2>$null | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Could not create ubs_db database." -ForegroundColor Red
            $env:PGPASSWORD = $prev
            return $false
        }
    }

    & $Psql -U postgres -h localhost -c "GRANT ALL PRIVILEGES ON DATABASE ubs_db TO ubs_user" 2>$null | Out-Null
    $env:PGPASSWORD = $prev

    if (-not (Test-UbsDatabase -Psql $Psql)) {
        Write-Host "Database setup finished but ubs_db is still not reachable." -ForegroundColor Red
        return $false
    }

    Write-Host "Database ubs_db created." -ForegroundColor Green
    return $true
}

$psql = Find-Psql
if (-not $psql) {
    Write-Host "psql not found. Add PostgreSQL bin to PATH or run scripts\setup-database.sql in pgAdmin." -ForegroundColor Red
    exit 1
}

if (-not (Initialize-UbsDatabase -Psql $psql)) {
    exit 1
}

Write-Host "PostgreSQL ready (ubs_db @ localhost:5432) — tables are created by the app on startup" -ForegroundColor Green
Write-Host "Starting Utility Billing System..." -ForegroundColor Cyan
Write-Host "Swagger UI: http://localhost:8080/api/swagger-ui.html" -ForegroundColor Green
Write-Host "Demo logins: admin/Admin@123 | operator/Operator@123 | finance/Finance@123 | customer/Customer@123" -ForegroundColor Yellow
Write-Host "OTP/notifications: @ubs.local demo users -> hirwareponse04@gmail.com" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop the server." -ForegroundColor DarkGray

.\mvnw.cmd spring-boot:run
