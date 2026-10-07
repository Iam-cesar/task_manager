package com.example.task_manager.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.task_manager.dtos.output.ProjectResponseDto;
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

    @Query("""
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

	@Query("""
		SELECT DISTINCT p FROM ProjectModel p
		LEFT JOIN FETCH p.project_owner
		LEFT JOIN FETCH p.members
		WHERE p.id IN :ids
		ORDER BY p.name ASC
	""")
	List<ProjectModel> findAllWithRelationsByIdIn(@Param("ids") List<Integer> ids);
}
