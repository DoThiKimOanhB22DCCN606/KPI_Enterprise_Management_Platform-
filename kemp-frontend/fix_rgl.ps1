$ErrorActionPreference = 'Stop'

$content = Get-Content src/pages/DashboardBuilderPage.tsx -Raw
$content = $content -replace "import \{ Responsive, WidthProvider \} from 'react-grid-layout';`r?`nimport type \{ Layout \} from 'react-grid-layout';", "import RGL from 'react-grid-layout';`n`ntype Layout = RGL.Layout;`nconst ResponsiveGridLayout = RGL.WidthProvider(RGL.Responsive);"
$content = $content -replace "const ResponsiveGridLayout = WidthProvider\(Responsive\);`r?`n", ""

Set-Content src/pages/DashboardBuilderPage.tsx $content -NoNewline
Write-Host "Replaced imports"
