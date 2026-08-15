package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.policy.CompanySort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

/**
 * Bounded read model for Company list/search operations.
 *
 * <p>Sort expressions are selected only from {@link CompanySort}; no client-provided identifier is
 * concatenated into SQL. Search values are parameters and LIKE wildcards are escaped.
 */
@Repository
public class CompanyQueryRepository {

    private static final char LIKE_ESCAPE = '\\';

    private final EntityManager entityManager;

    public CompanyQueryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<CompanyEntity> search(String search, int page, int size, CompanySort sort) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<CompanyEntity> query = builder.createQuery(CompanyEntity.class);
        Root<CompanyEntity> company = query.from(CompanyEntity.class);
        Predicate predicate = searchPredicate(builder, company, search);
        query.where(predicate);
        query.orderBy(orderBy(builder, company, sort));
        List<CompanyEntity> rows = entityManager.createQuery(query)
                .setFirstResult(Math.multiplyExact(page, size))
                .setMaxResults(size)
                .getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<CompanyEntity> countCompany = countQuery.from(CompanyEntity.class);
        countQuery.select(builder.count(countCompany));
        countQuery.where(searchPredicate(builder, countCompany, search));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(rows, PageRequest.of(page, size), total);
    }

    private Predicate searchPredicate(
            CriteriaBuilder builder,
            Root<CompanyEntity> company,
            String search) {
        if (search == null) {
            return builder.conjunction();
        }

        String pattern = "%" + escapeLike(search.toLowerCase(java.util.Locale.ROOT)) + "%";
        return builder.or(
                builder.like(builder.lower(company.get("name")), pattern, LIKE_ESCAPE),
                builder.like(builder.lower(company.get("contactPerson")), pattern, LIKE_ESCAPE),
                builder.like(builder.lower(company.get("contactEmail")), pattern, LIKE_ESCAPE),
                builder.like(builder.lower(company.get("contactPhone")), pattern, LIKE_ESCAPE));
    }

    private List<jakarta.persistence.criteria.Order> orderBy(
            CriteriaBuilder builder,
            Root<CompanyEntity> company,
            CompanySort sort) {
        jakarta.persistence.criteria.Order primary = switch (sort) {
            case NAME_ASC -> builder.asc(company.get("name"));
            case NAME_DESC -> builder.desc(company.get("name"));
            case UPDATED_AT_DESC -> builder.desc(company.get("updatedAt"));
        };
        return List.of(primary, builder.asc(company.get("id")));
    }

    private String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
