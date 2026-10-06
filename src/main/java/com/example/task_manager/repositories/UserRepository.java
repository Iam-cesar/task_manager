package com.example.task_manager.repositories;

import java.util.List;
import java.util.Optional;

import com.example.task_manager.models.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.UserModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

    Optional<UserModel> findByEmail(String email);

	@Query(value = """
		SELECT u.*
		FROM TB_USERS u
		WHERE to_tsvector(
			'simple',
			coalesce(u.name, '') || ' ' || coalesce(u.email, '')
		) @@ websearch_to_tsquery('simple', :search)
		ORDER BY u.name ASC
	""", nativeQuery = true)
	public List<UserModel> searchByNameAndEmail(@Param("search") String aSearch);

}
