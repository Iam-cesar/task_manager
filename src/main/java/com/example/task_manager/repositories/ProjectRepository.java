package com.example.task_manager.repositories;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.task_manager.models.ProjectModel;

public interface ProjectRepository extends JpaRepository<ProjectModel, Integer> {

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ProjectModel p WHERE p.project_owner.id = :ownerId AND p.name = :name
    """)
    boolean existsByProjectOwnerIdAndName(
        @Param("ownerId") Integer ownerId,
        @Param("name") String aName
    );

    @Query(value = """
        SELECT DISTINCT p FROM ProjectModel p
        LEFT JOIN FETCH p.project_owner
        LEFT JOIN FETCH p.members
    """)
    List<ProjectModel> findAllWithRelations();

    @Query("""
        SELECT DISTINCT p FROM ProjectModel p
        LEFT JOIN FETCH p.project_owner
        LEFT JOIN FETCH p.members
        WHERE p.id = :id
    """)
    Optional<ProjectModel> findByIdWithRelations(@Param("id") Integer anId);

	@Query(value = """
		SELECT p.id
		FROM TB_PROJECTS p
		WHERE to_tsvector(
			'simple',
			coalesce(p.name, '') || ' ' || coalesce(p.description, '')
		) @@ websearch_to_tsquery('simple', :name)
		ORDER BY p.name ASC
	""", nativeQuery = true)
	List<Integer> searchIdsByNameAndDescription(@Param("name") String aName);

	@Query(
		value = """
			SELECT p.id
			FROM TB_PROJECTS p
			WHERE to_tsvector(
				'simple',
				coalesce(p.name, '') || ' ' || coalesce(p.description, '')
			) @@ websearch_to_tsquery('simple', :name)
			ORDER BY p.name ASC, p.id ASC
		""",
		countQuery = """
			SELECT COUNT(*)
			FROM TB_PROJECTS p
			WHERE to_tsvector(
				'simple',
				coalesce(p.name, '') || ' ' || coalesce(p.description, '')
			) @@ websearch_to_tsquery('simple', :name)
		""",
		nativeQuery = true
	)
	Page<Integer> searchIdsByNameAndDescription(
		@Param("name") String aName,
		Pageable pageable
	);

	@Query("""
		SELECT DISTINCT p FROM ProjectModel p
		LEFT JOIN FETCH p.project_owner
		LEFT JOIN FETCH p.members
		WHERE p.id IN :ids
		ORDER BY p.name ASC
	""")
	List<ProjectModel> findAllWithRelationsByIdIn(@Param("ids") List<Integer> ids);

	@Query(value = """
		SELECT p.id
		FROM ProjectModel p
		ORDER BY p.name ASC, p.id ASC
	""",
	countQuery = """
		SELECT COUNT(p) FROM ProjectModel p
	""")
	Page<Integer> findAllIds(Pageable pageable);
}
