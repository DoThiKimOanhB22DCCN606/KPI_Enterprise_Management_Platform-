# Test Text-to-SQL endpoint
Write-Host "Testing Text-to-SQL..."
$sqlResponse = Invoke-WebRequest -Uri "http://localhost:8089/v1/ai/query" `
    -Method Post `
    -Headers @{ "Content-Type" = "application/json" } `
    -Body '{"prompt": "Show me the top performing departments in Q1", "tenantId": "00000000-0000-0000-0000-000000000001"}' `
    -ErrorAction Stop

$sqlResponse.Content | ConvertFrom-Json | Format-List


# Test Recommendation endpoint
Write-Host "`nTesting KPI Recommendations..."
$recommendationBody = @{
    departmentType = "Sales"
    historicalMetrics = @{
        revenue_growth = "15%"
        customer_retention = "92%"
        attrition = "5%"
    }
} | ConvertTo-Json

$recResponse = Invoke-WebRequest -Uri "http://localhost:8089/v1/ai/recommendations/kpi" `
    -Method Post `
    -Headers @{ "Content-Type" = "application/json" } `
    -Body $recommendationBody `
    -ErrorAction Stop

$recResponse.Content | ConvertFrom-Json | Format-List
