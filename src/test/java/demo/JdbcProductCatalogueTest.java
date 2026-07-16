package demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ContextConfiguration;

@JdbcTest
@ContextConfiguration(initializers = TestDemoAccess.class)
@Import(JdbcProductCatalogue.class)
class JdbcProductCatalogueTest {

    @Autowired
    private ProductCatalogue catalogue;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void loadsAtLeastOneHundredValidCatalogueProducts() {
        assertThat(catalogue.allProducts())
                .hasSizeGreaterThanOrEqualTo(100);
    }

    @Test
    void generatesAnIdentityForANewProduct() {
        jdbcClient.sql("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('generated-identity-product', 'Generated identity product', 1, 'g', 1.00)
                """).update();

        assertThat(jdbcClient.sql("""
                SELECT id
                FROM product
                WHERE slug = 'generated-identity-product'
                """).query(Long.class).single())
                .isPositive();
    }

    @Test
    void rejectsAProductWithADuplicateIdentity() {
        assertThatProductIsRejected("""
                INSERT INTO product (id, slug, name, package_quantity, package_unit, price)
                VALUES (1, 'duplicate-identity', 'Duplicate identity product', 1, 'g', 1.00)
                """);
    }

    @Test
    void rejectsAProductWithoutASlug() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES (NULL, 'Invalid product', 500, 'g', 1.49)
                """);
    }

    @Test
    void rejectsAProductWithANonLowercaseKebabCaseSlug() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('Wholewheat-spaghetti-500g', 'Invalid product', 500, 'g', 1.49)
                """);
    }

    @Test
    void rejectsAProductWithADuplicateSlug() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('wholewheat-spaghetti-500g', 'Duplicate product', 500, 'g', 1.49)
                """);
    }

    @Test
    void rejectsAProductWithoutAName() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('missing-name', NULL, 500, 'g', 1.49)
                """);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void rejectsAProductWithABlankName(final String blankName) {
        assertThatProductIsRejected("""
                    INSERT INTO product (slug, name, package_quantity, package_unit, price)
                    VALUES ('blank-name-product', '%s', 500, 'g', 1.49)
                    """.formatted(blankName));
    }

    @Test
    void rejectsAProductWithoutAPackageQuantity() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('missing-package-quantity', 'Invalid product', NULL, 'g', 1.49)
                """);
    }

    @Test
    void rejectsAProductWithANonPositivePackageQuantity() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('zero-package-quantity', 'Invalid product', 0, 'g', 1.49)
                """);
    }

    @Test
    void rejectsAProductWithoutAPackageUnit() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('missing-package-unit', 'Invalid product', 500, NULL, 1.49)
                """);
    }

    @Test
    void rejectsAProductWithAnUnsupportedPackageUnit() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('unsupported-package-unit', 'Invalid product', 500, 'l', 1.49)
                """);
    }

    @Test
    void rejectsAProductWithoutAPrice() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('missing-price', 'Invalid product', 500, 'g', NULL)
                """);
    }

    @Test
    void rejectsAProductWithANonPositivePrice() {
        assertThatProductIsRejected("""
                INSERT INTO product (slug, name, package_quantity, package_unit, price)
                VALUES ('zero-price', 'Invalid product', 500, 'g', 0)
                """);
    }

    private void assertThatProductIsRejected(final String sql) {
        assertThatThrownBy(() -> jdbcClient.sql(sql).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
