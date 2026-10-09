package com.example.task_manager.repositories;

import java.util.List;
import java.util.Optional;

import com.example.task_manager.models.TaskModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task_manager.models.UserModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

    Optional<UserModel> findByEmail(String email);

	@Query(value = """
		SELECT u.id
		FROM TB_USERS u
		WHERE to_tsvector(
			'simple',
			coalesce(u.name, '') || ' ' || coalesce(u.email, '')
		) @@ websearch_to_tsquery('simple', :search)
		ORDER BY u.name ASC, u.id ASC
		""",
		countQuery = """
			SELECT COUNT(*)
			FROM TB_USERS u
			WHERE to_tsvector(
				'simple',
				coalesce(u.name, '') || ' ' || coalesce(u.email, '')
			) @@ websearch_to_tsquery('simple', :search)
		""",
		nativeQuery = true
	)
	Page<Number> searchByNameAndEmail(@Param("search") String search, Pageable pageable);

	@Query(
		value = "SELECT u.id FROM UserModel u ORDER BY u.name ASC, u.id ASC",
		countQuery = "SELECT COUNT(u) FROM UserModel u"
	)
	Page<Number> findAllIds(Pageable pageable);
}
