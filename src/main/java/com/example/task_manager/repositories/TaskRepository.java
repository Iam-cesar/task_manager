package com.example.task_manager.repositories;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.TaskModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskModel, Integer> {

	@Query(value = """
		SELECT t.id
		FROM TB_TASKS t
		WHERE to_tsvector(
			'simple',
			coalesce(t.title, '') || ' ' || coalesce(t.description, '')
		) @@ websearch_to_tsquery('simple', :search)
		ORDER BY t.title ASC
	""", nativeQuery = true)
	List<Integer> searchIdsByTitleAndDescription(@Param("search") String aSearch);

	@Query("""
		SELECT DISTINCT t FROM TaskModel t
		JOIN FETCH t.project p
		LEFT JOIN FETCH p.project_owner
		LEFT JOIN FETCH p.members
		JOIN FETCH t.user
		LEFT JOIN FETCH t.taskLabels taskLabel
		LEFT JOIN FETCH taskLabel.label
		WHERE t.id IN :ids
		ORDER BY t.title ASC
	""")
	List<TaskModel> findAllWithRelationsByIdIn(@Param("ids") List<Integer> ids);

	@Query("""
		SELECT DISTINCT t FROM TaskModel t
		JOIN FETCH t.project p
		LEFT JOIN FETCH p.project_owner
		LEFT JOIN FETCH p.members
		JOIN FETCH t.user
		LEFT JOIN FETCH t.taskLabels taskLabel
		LEFT JOIN FETCH taskLabel.label
		WHERE t.id = :id
	""")
	Optional<TaskModel> findWithRelationsById(@Param("id") Integer id);

	@Query("""
		SELECT DISTINCT t FROM TaskModel t
		JOIN FETCH t.project p
		LEFT JOIN FETCH p.project_owner
		LEFT JOIN FETCH p.members
		JOIN FETCH t.user
		LEFT JOIN FETCH t.taskLabels taskLabel
		LEFT JOIN FETCH taskLabel.label
		ORDER BY t.title ASC
	""")
	List<TaskModel> findAllWithRelations();

	TaskModel findByStatus(@NonNull final String aStatus);
}
