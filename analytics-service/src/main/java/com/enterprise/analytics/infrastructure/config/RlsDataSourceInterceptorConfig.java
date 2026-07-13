package com.enterprise.analytics.infrastructure.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

@Configuration
public class RlsDataSourceInterceptorConfig {

    @Bean
    public BeanPostProcessor dataSourceWrapper() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSource) {
                    return new DelegatingDataSource((DataSource) bean) {
                        @Override
                        public Connection getConnection() throws SQLException {
                            Connection conn = super.getConnection();
                            setTenantContext(conn);
                            return conn;
                        }

                        @Override
                        public Connection getConnection(String username, String password) throws SQLException {
                            Connection conn = super.getConnection(username, password);
                            setTenantContext(conn);
                            return conn;
                        }

                        private void setTenantContext(Connection conn) throws SQLException {
                            UUID tenantId = TenantContext.getTenantId();
                            if (tenantId != null) {
                                try (Statement stmt = conn.createStatement()) {
                                    stmt.execute("SET app.current_tenant_id = '" + tenantId + "'");
                                }
                            }
                        }
                    };
                }
                return bean;
            }
        };
    }
}
