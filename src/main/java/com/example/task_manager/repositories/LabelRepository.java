package com.example.task_manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.LabelModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<LabelModel, Integer> {

	@Query(value = """
		SELECT l.id
		FROM TB_LABELS l
		WHERE to_tsvector('simple', l.name) @@ websearch_to_tsquery('simple', :name)
		ORDER BY l.name ASC
	""", nativeQuery = true)
	List<Integer> searchIdsByName(@Param("name") String name);

	@Query("""
		SELECT DISTINCT l FROM LabelModel l
		LEFT JOIN FETCH l.taskLabels taskLabel
		LEFT JOIN FETCH taskLabel.task
		WHERE l.id IN :ids
		ORDER BY l.name ASC
	""")
	List<LabelModel> findAllWithTasksByIdIn(@Param("ids") List<Integer> ids);

	@Query("""
		SELECT DISTINCT l FROM LabelModel l
		LEFT JOIN FETCH l.taskLabels taskLabel
		LEFT JOIN FETCH taskLabel.task
		ORDER BY l.name ASC
	""")
	List<LabelModel> findAllWithTasks();

	@Query("""
		SELECT DISTINCT l FROM LabelModel l
		LEFT JOIN FETCH l.taskLabels taskLabel
		LEFT JOIN FETCH taskLabel.task
		WHERE l.id = :id
	""")
	Optional<LabelModel> findByIdWithTasks(@Param("id") Integer id);
}
