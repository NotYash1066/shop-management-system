package com.shopmanagement.repository;

import com.shopmanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

		Employee findByName(String name);

		Employee findByIdAndName(Long id, String name);

		Employee findByIdAndNameAndRoleIsNotNull(Long id, String name);

		Employee findByEmail(String email);

        java.util.List<Employee> findByShopId(Long shopId);

        java.util.Optional<Employee> findByIdAndShopId(Long id, Long shopId);
}
