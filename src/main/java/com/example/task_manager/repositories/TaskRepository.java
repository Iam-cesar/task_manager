package com.example.task_manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.TaskModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskModel, Integer> {

	@Query(value = """
		SELECT t.*
		FROM TB_TASKS t
		WHERE to_tsvector(
			'simple',
			coalesce(t.title, '') || ' ' || coalesce(t.description, '')
		) @@ websearch_to_tsquery('simple', :search)
		ORDER BY t.title ASC
	""", nativeQuery = true)
	public List<TaskModel> searchByTitleAndDescription(@Param("search") String aSearch);

}
