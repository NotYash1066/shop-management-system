package com.shopmanagement.rest;

import com.shopmanagement.entity.Employee;
import com.shopmanagement.repository.EmployeeRepository;
import com.shopmanagement.services.CurrentUserService;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasAuthority('employee:read')")
@Transactional
public class EmployeeController {

	private final EmployeeRepository employeeRepository;
    private final com.shopmanagement.repository.ShopRepository shopRepository;
    private final CurrentUserService currentUserService;

    public EmployeeController(
            EmployeeRepository employeeRepository,
            com.shopmanagement.repository.ShopRepository shopRepository,
            CurrentUserService currentUserService) {
        this.employeeRepository = employeeRepository;
        this.shopRepository = shopRepository;
        this.currentUserService = currentUserService;
    }

	@GetMapping
	public List<Employee> getAllEmployees() {
		return employeeRepository.findByShopId(currentUserService.getCurrentShopId());
	}

	@GetMapping("/{id}")
	public Employee getEmployeeById(@PathVariable Long id) {
		return employeeRepository.findByIdAndShopId(id, currentUserService.getCurrentShopId())
					.orElseThrow(() -> new RuntimeException("Employee not found"));
	}

	@PostMapping
    @PreAuthorize("hasAuthority('employee:write')")
	public Employee createEmployee(@RequestBody Employee employee) {
        employee.setShop(shopRepository.getReferenceById(currentUserService.getCurrentShopId()));
			return employeeRepository.save(employee);
	}

	@PutMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:write')")
	public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        employeeRepository.findByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
			employee.setId(id);
        employee.setShop(shopRepository.getReferenceById(currentUserService.getCurrentShopId()));
			return employeeRepository.save(employee);
	}

	@DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('employee:write')")
	public String deleteEmployee(@PathVariable Long id) {
        Employee existing = employeeRepository.findByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
			employeeRepository.delete(existing);
			return "Employee deleted";
	}
}
