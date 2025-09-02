package com.luv2code.springboot.cruddemo.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.luv2code.springboot.cruddemo.entity.Employee;
import com.luv2code.springboot.cruddemo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EmployeeRestController {

    private EmployeeService employeeService;

    private ObjectMapper objectMapper;

    @Autowired
    public EmployeeRestController(EmployeeService theEmployeeService, ObjectMapper theObjectMapper) {
        employeeService = theEmployeeService;
        objectMapper = theObjectMapper;
    }

    // expose "/employees" and return a list of employees
    @GetMapping("/employees")
    public List<Employee> findAll() {
        return employeeService.findAll();
    }

    // add mapping for GET /employees/{employeeId}

    @GetMapping("/employees/{employeeId}")
    public Employee getEmployee(@PathVariable int employeeId) {

        Employee theEmployee = employeeService.findById(employeeId);

        if (theEmployee == null) {
            throw new RuntimeException("Employee id not found - " + employeeId);
        }

        return theEmployee;
    }

    // add mapping for POST /employees - add new employee

    @PostMapping("/employees")
    public Employee addEmployee(@RequestBody Employee theEmployee) {

        // also just in case they pass an id in JSON ... set id to 0
        // this is to force a save of new item ... instead of update

        theEmployee.setId(0);

        Employee dbEmployee = employeeService.save(theEmployee);

        return dbEmployee;
    }

    // add mapping for PUT /employees - update existing employee

    @PutMapping("/employees")
    public Employee updateEmployee(@RequestBody Employee theEmployee) {

        Employee dbEmployee = employeeService.save(theEmployee);

        return dbEmployee;
    }

    // add mapping for PATCH /employees/{employeeId} - patch employee ... partial update

    @PatchMapping("/employees/{employeeId}")
    public Employee patchEmployee(@PathVariable int employeeId,
                                  @RequestBody Map<String, Object> patchPayload) {

        Employee tempEmployee = employeeService.findById(employeeId);

        // throw exception if null
        if (tempEmployee == null) {
            throw new RuntimeException("Employee id not found - " + employeeId);
        }

        // throw exception if request body contains "id" key
        if (patchPayload.containsKey("id")) {
            throw new RuntimeException("Employee id not allowed in request body - " + employeeId);
        }

        Employee patchedEmployee = apply(patchPayload, tempEmployee);

        Employee dbEmployee = employeeService.save(patchedEmployee);

        return dbEmployee;
    }

    private Employee apply(Map<String, Object> patchPayload, Employee tempEmployee) {

        // Convert employee object to a JSON object node
        ObjectNode employeeNode = objectMapper.convertValue(tempEmployee, ObjectNode.class);

        // Convert the patchPayload map to a JSON object node
        ObjectNode patchNode = objectMapper.convertValue(patchPayload, ObjectNode.class);

        // Merge the patch updates into the employee node
        employeeNode.setAll(patchNode);

        return objectMapper.convertValue(employeeNode, Employee.class);
    }

    // add mapping for DELETE /employees/{employeeId} - delete employee

    @DeleteMapping("/employees/{employeeId}")
    public String deleteEmployee(@PathVariable int employeeId) {

        Employee tempEmployee = employeeService.findById(employeeId);

        // throw exception if null

        if (tempEmployee == null) {
            throw new RuntimeException("Employee id not found - " + employeeId);
        }

        employeeService.deleteById(employeeId);

        return "Deleted employee id - " + employeeId;
    }

}

/*
    burası REST Controller katmanı. Yani dış dünyadan (örn. Postman, frontend, mobil uygulama)
    gelen HTTP isteklerini karşılıyor, doğru service metodunu çağırıp sonucu JSON olarak döndürüyor


    @RestController
    @RequestMapping("/api")
    public class EmployeeRestController { ... }
	•	@RestController: Sınıfın tüm metotlarının JSON döndürmesini sağlar (her metoda ayrıca
	    @ResponseBody yazmana gerek yok).
	•	@RequestMapping("/api"): Bu controller altındaki tüm endpoint’lerin başına /api prefix’i
	     eklenir (örn. /api/employees).

    --------------------------------

    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;

    •	Service katmanı enjekte ediliyor. Controller iş kuralı yazmaz; sadece isteği yönlendirir.
	•	ObjectMapper: PATCH için gelen kısmi güncellemeyi entity’ye birleştirmekte kullanılıyor.

    --------------------------------
    Endpoint’ler (CRUD + PATCH)

    1) Tüm çalışanları getir

    @GetMapping("/employees")
    public List<Employee> findAll() {
    return employeeService.findAll();
    }
    •	GET /api/employees
	•	Tüm Employee kayıtlarını JSON listesi olarak döner.


    2) Tek çalışan getir (ID ile)
    @GetMapping("/employees/{employeeId}")
    public Employee getEmployee(@PathVariable int employeeId) { ... }

    •	GET /api/employees/{id}
	•	Yol parametresi employeeId alınır, service.findById ile bulunur.
	•	Bulunamazsa RuntimeException fırlatıyor (bunu aşağıda “İyileştirme” kısmında ele alacağım).

    3) Yeni çalışan ekle
    @PostMapping("/employees")
    public Employee addEmployee(@RequestBody Employee theEmployee) {
    theEmployee.setId(0);
    return employeeService.save(theEmployee);
}
    	•	POST /api/employees
	•	Body: Employee JSON’ı.
	•	setId(0): Spring Data JPA save() metodu ID 0/ null ise insert, dolu ise update yapar. Burada
	    “gelen JSON’da yanlışlıkla id varsa bile yeni kayıt olsun” diye ID sıfırlanıyor.
	•	Not: Rest standartlarında genelde 201 Created ve Location header’ı dönmek güzel olur
	       (aşağıda öneri var).

    4) Varolan çalışanı güncelle (tam)

    @PutMapping("/employees")
    public Employee updateEmployee(@RequestBody Employee theEmployee) {
    return employeeService.save(theEmployee);
}
    •	PUT /api/employees
	•	Body: Tam Employee objesi (ID dolu olmalı).
	•	save() ID doluysa update yapar.

    5) Kısmi güncelleme (PATCH)
    @PatchMapping("/employees/{employeeId}")
    public Employee patchEmployee(@PathVariable int employeeId,
                              @RequestBody Map<String, Object> patchPayload) { ... }

	•	PATCH /api/employees/{id}
	•	Body: Sadece değişecek alanlar (ör. {"firstName":"Ali"}).
	•	Önce kayıt var mı kontrol ediliyor; sonra id alanı gövdede geldiyse reddediliyor
	    (güvenlik/ bütünlük için doğru).
	•	apply(...):
	•	tempEmployee → ObjectNode’a çevrilir.
	•	patchPayload → ObjectNode’a çevrilir.
	•	employeeNode.setAll(patchNode) ile merge yapılır.
	•	Tekrar Employee nesnesine dönüştürülür ve save() çağrılır.
	•	Bu pratik bir merge yöntemi; ama tip/validasyon hatalarına dikkat (aşağıda öneriler).








 */












