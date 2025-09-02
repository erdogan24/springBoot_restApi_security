package com.luv2code.springboot.cruddemo.dao;

import com.luv2code.springboot.cruddemo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    // that's it ... no need to write any code LOL!


}
/*
    DAO Katmanı Nedir?
    •	DAO (Data Access Object): Veritabanına erişim katmanıdır.
	•	Amaç: Veritabanı işlemlerini (insert, update, delete, select) ayrı bir katmanda tutarak kodu daha düzenli ve bakımı kolay hale getirmektir.
    •	Normalde burada SQL sorguları veya EntityManager kodları olurdu.

	•	Burada interface tanımlanıyor, class değil. Çünkü Spring Data JPA bize hazır bir kütüphane sunuyor.
	•	Sen sadece hangi entity (Employee) ve id türü (Integer) ile çalışacağını söylüyorsun.
	•	Geri kalan CRUD (Create, Read, Update, Delete) operasyonlarını Spring otomatik olarak sağlıyor.

        Yani bu interface’i yazdığında, Spring senin için şunları otomatik yapar:
	•	save() → yeni kayıt ekleme/güncelleme
	•	findById() → id’ye göre bulma
	•	findAll() → tüm kayıtları getirme
	•	deleteById() → id’ye göre silme

	    JpaRepository<T, ID> → Spring Data JPA’nın sağladığı hazır bir interface’tir.
	•	T → Entity class’ını belirtir (Employee)
	•	ID → Primary key türünü belirtir (Integer)

	    JpaRepository içinde yüzlerce hazır metod vardır:
	•	List<Employee> findAll()
	•	Optional<Employee> findById(Integer id)
	•	Employee save(Employee e)
	•	void deleteById(Integer id)
	•	long count()

        Bunların hepsi hiçbir kod yazmadan kullanılabilir.

        Neden Kullanıyoruz?
	•	Kod tekrarını önlemek: Her defasında SQL yazmak zorunda değilsin.
	•	Bakımı kolay: Repository’leri kullanmak daha düzenli bir mimari sağlar.
	•	Query Method desteği: Sadece method adıyla özel sorgular yazabilirsin.

 */