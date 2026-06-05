# Start UBS locally with embedded H2 and demo seed data (Swagger-ready)
Set-Location $PSScriptRoot
Write-Host "Starting Utility Billing System (local profile)..." -ForegroundColor Cyan
Write-Host "Swagger UI: http://localhost:8080/api/swagger-ui.html" -ForegroundColor Green
Write-Host "Demo logins: admin/Admin@123 | operator/Operator@123 | finance/Finance@123 | customer/Customer@123" -ForegroundColor Yellow
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
