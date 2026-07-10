package demo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import module java.base;
import module java.sql;

@Repository
class JdbcProductCatalogue implements ProductCatalogue {

    private final JdbcClient jdbcClient;

    JdbcProductCatalogue(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Product> allProducts() {
        return jdbcClient.sql("""
                SELECT slug, name, package_quantity, package_unit, price, image_filename
                FROM product
                ORDER BY id
                """)
                .query(this::mapProduct)
                .list();
    }

    private Product mapProduct(final ResultSet resultSet, final int rowNumber) throws SQLException {
        return new Product(
                resultSet.getString("slug"),
                resultSet.getString("name"),
                resultSet.getInt("package_quantity"),
                MeasurementUnit.from(resultSet.getString("package_unit")),
                resultSet.getObject("price", BigDecimal.class),
                resultSet.getString("image_filename"));
    }
}
