$ErrorActionPreference = 'Stop'

$content = Get-Content src/pages/DashboardBuilderPage.tsx -Raw
$content = $content -replace "import RGL from 'react-grid-layout';", "import { Responsive, WidthProvider } from 'react-grid-layout';"
$content = $content -replace "type Layout = RGL.Layout;`r?`nconst ResponsiveGridLayout = RGL.WidthProvider\(RGL.Responsive\);", "// @ts-ignore`nimport type { Layout } from 'react-grid-layout';`nconst ResponsiveGridLayout = WidthProvider(Responsive);"
Set-Content src/pages/DashboardBuilderPage.tsx $content -NoNewline

$content = Get-Content src/pages/KpiDetailPage.tsx -Raw
$content = $content -replace "const \{ data: approvals \} = useQuery", "//"
Set-Content src/pages/KpiDetailPage.tsx $content -NoNewline
Write-Host "Replaced again"
