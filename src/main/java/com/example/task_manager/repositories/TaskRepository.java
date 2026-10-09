package com.example.task_manager.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.TaskModel;
import com.example.task_manager.enums.TaskStatusEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskModel, Integer> {

	@Query(value = """
		SELECT t.id
		FROM TB_TASKS t
		WHERE t.archived = false
			AND to_tsvector(
			'simple',
			coalesce(t.title, '') || ' ' || coalesce(t.description, '')
		) @@ websearch_to_tsquery('simple', :search)
		ORDER BY t.title ASC
	""",
		countQuery = """
			SELECT COUNT(*)
			FROM TB_TASKS t
			WHERE t.archived = false
				AND to_tsvector(
				'simple',
				coalesce(t.title, '') || ' ' || coalesce(t.description, '')
			) @@ websearch_to_tsquery('simple', :search)
		""",
		nativeQuery = true)
	Page<Number> searchIdsByTitleAndDescription(@Param("search") String search, Pageable pageable);

	@Query(value = """
		SELECT t.id
		FROM TB_TASKS t
		WHERE t.archived = false
			AND t.due_date < CURRENT_TIMESTAMP
			AND t.status NOT IN ('COMPLETED', 'CANCELED')
			AND to_tsvector(
				'simple',
				coalesce(t.title, '') || ' ' || coalesce(t.description, '')
			) @@ websearch_to_tsquery('simple', :search)
		ORDER BY t.title ASC, t.id ASC
		""",
		countQuery = """
			SELECT COUNT(*)
			FROM TB_TASKS t
			WHERE t.archived = false
				AND t.due_date < CURRENT_TIMESTAMP
				AND t.status NOT IN ('COMPLETED', 'CANCELED')
				AND to_tsvector(
					'simple',
					coalesce(t.title, '') || ' ' || coalesce(t.description, '')
				) @@ websearch_to_tsquery('simple', :search)
		""",
		nativeQuery = true)
	Page<Number> searchOverdueIdsByTitleAndDescription(
		@Param("search") String search,
		Pageable pageable
	);

	@Query("""
		SELECT DISTINCT t FROM TaskModel t
		LEFT JOIN FETCH t.project p
		LEFT JOIN FETCH p.project_owner
		LEFT JOIN FETCH p.members
		LEFT JOIN FETCH t.user
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
		WHERE t.archived = false
		ORDER BY t.title ASC
	""")
	List<TaskModel> findAllWithRelations();

	@Query(
		value = "SELECT t.id FROM TaskModel t WHERE t.archived = false ORDER BY t.title ASC, t.id ASC",
		countQuery = "SELECT COUNT(t) FROM TaskModel t WHERE t.archived = false"
	)
	Page<Number> findAllIds(Pageable pageable);

	@Query("""
		SELECT t.id FROM TaskModel t
		WHERE t.archived = false
			AND t.due_date < CURRENT_TIMESTAMP
			AND t.status NOT IN (com.example.task_manager.enums.TaskStatusEnum.COMPLETED,
				com.example.task_manager.enums.TaskStatusEnum.CANCELED)
		ORDER BY t.title ASC, t.id ASC
		""")
	Page<Number> findOverdueIds(Pageable pageable);

	@Query("""
		SELECT t.status AS status, COUNT(t) AS count
		FROM TaskModel t
		WHERE t.archived = false
		GROUP BY t.status
		""")
	List<TaskStatusCountProjection> countNonArchivedTasksByStatus();

	@Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TaskModel t WHERE t.user.id = :userId")
	boolean existsAssignedTasksForUser(@Param("userId") Integer userId);
}
