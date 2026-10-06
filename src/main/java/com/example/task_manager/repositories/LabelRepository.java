package com.example.task_manager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.LabelModel;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabelRepository extends JpaRepository<LabelModel, Integer> {

	@Query(value = """
		SELECT l.*
		FROM TB_LABELS l
		WHERE to_tsvector('simple', l.name) @@ websearch_to_tsquery('simple', :name)
		ORDER BY l.name ASC
	""", nativeQuery = true)
	List<LabelModel> findByName(String name);
}
