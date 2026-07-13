$ErrorActionPreference = 'Stop'

# KpiListPage
$content = Get-Content src/pages/KpiListPage.tsx -Raw
$content = $content -replace "import React, \{ useState \} from 'react';", "import { useState } from 'react';"
$content = $content -replace "import \{ DataTable, Column \}", "import { DataTable } from '../components/DataTable';`nimport type { Column }"
$content = $content -replace "const \[page, setPage\] = useState\(0\);", "const [page] = useState(0);"
$content = $content -replace "const \{ data, isLoading, error \} = useQuery", "const { data, isLoading } = useQuery"
Set-Content src/pages/KpiListPage.tsx $content -NoNewline

# KpiDetailPage
$content = Get-Content src/pages/KpiDetailPage.tsx -Raw
$content = $content -replace "import React, \{ useState \} from 'react';`r?`n", ""
$content = $content -replace "const \{ data: approvals \} = useQuery\(\{\s+queryKey: \['kpi-approvals', id\],\s+queryFn: async \(\) => \(await apiClient\.get\(`/v1/kpis/`\$\{id\}/approvals`\)\)\.data\s+\}\);", "// Removed unused approvals query"
Set-Content src/pages/KpiDetailPage.tsx $content -NoNewline

# LeaderboardPage
$content = Get-Content src/pages/LeaderboardPage.tsx -Raw
$content = $content -replace "import React, \{ useState \} from 'react';", "import { useState } from 'react';"
$content = $content -replace "data\?\.map\(\(entry, idx\) => \{", "data?.map((entry) => {"
Set-Content src/pages/LeaderboardPage.tsx $content -NoNewline

# NotificationsPage
$content = Get-Content src/pages/NotificationsPage.tsx -Raw
$content = $content -replace "import React from 'react';`r?`n", ""
Set-Content src/pages/NotificationsPage.tsx $content -NoNewline

# ReportsPage
$content = Get-Content src/pages/ReportsPage.tsx -Raw
$content = $content -replace "import React, \{ useState \} from 'react';", "import { useState } from 'react';"
$content = $content -replace "import \{ DataTable, Column \}", "import { DataTable } from '../components/DataTable';`nimport type { Column }"
$content = $content -replace "import \{ FileText, Download, Plus \}", "import { Download, Plus }"
Set-Content src/pages/ReportsPage.tsx $content -NoNewline

# AdminUsersPage
$content = Get-Content src/pages/AdminUsersPage.tsx -Raw
$content = $content -replace "import React, \{ useState \} from 'react';", "import { useState } from 'react';"
$content = $content -replace "import \{ DataTable, Column \}", "import { DataTable } from '../components/DataTable';`nimport type { Column }"
Set-Content src/pages/AdminUsersPage.tsx $content -NoNewline

# AuditLogPage
$content = Get-Content src/pages/AuditLogPage.tsx -Raw
$content = $content -replace "import \{ DataTable, Column \} from '../components/DataTable';", "import type { Column } from '../components/DataTable';"
Set-Content src/pages/AuditLogPage.tsx $content -NoNewline

# DashboardBuilderPage
$content = Get-Content src/pages/DashboardBuilderPage.tsx -Raw
$content = $content -replace "import React, \{ useState \} from 'react';", "import { useState } from 'react';"
$content = $content -replace "import \{ Responsive, WidthProvider, Layout \} from 'react-grid-layout';", "import { Responsive, WidthProvider } from 'react-grid-layout';`nimport type { Layout } from 'react-grid-layout';"
$content = $content -replace "onLayoutChange = \(layout: Layout\[\], layouts", "onLayoutChange = (_layout: Layout[], layouts"
Set-Content src/pages/DashboardBuilderPage.tsx $content -NoNewline

Write-Host "Replaced stuff"
