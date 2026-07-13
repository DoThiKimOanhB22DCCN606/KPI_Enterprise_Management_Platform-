const fs = require('fs');
let content = fs.readFileSync('docker-compose.yml', 'utf8');

// 1. Revert JDBC URL
content = content.replace('SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}?stringtype=unspecified', 'SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}');

// 2. Comment out unnecessary services
const servicesToComment = [
    'goal-service', 
    'report-service', 
    'analytics-service', 
    'audit-service', 
    'dashboard-service', 
    'integration-service'
];

for (let srv of servicesToComment) {
    // Match from "  service-name:" to the end of its "networks:" block
    const regex = new RegExp(`  ${srv}:[\\s\\S]*?    networks:\\n      - enterprise-network`, 'm');
    const match = content.match(regex);
    if (match) {
        const commented = match[0].split('\n').map(line => '#' + line).join('\n');
        content = content.replace(match[0], commented);
    } else {
        console.warn(`Could not find block for ${srv}`);
    }
}

fs.writeFileSync('docker-compose.yml', content);
console.log('Successfully patched docker-compose.yml');
