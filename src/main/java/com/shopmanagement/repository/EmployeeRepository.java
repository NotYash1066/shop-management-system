package com.shopmanagement.repository;

import com.shopmanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Employee findByName(String name);

	Employee findByIdAndName(Long id, String name);

	Employee findByIdAndNameAndRoleIsNotNull(Long id, String name);

	Employee findByEmail(String email);
}