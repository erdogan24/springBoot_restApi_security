package com.luv2code.springboot.cruddemo.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;


@Configuration
public class DemoSecurityConfig {


    // add support for JDBC ... no more hardcoded users

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource){

        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        // define query to retrieve a user by username
        jdbcUserDetailsManager.setUsersByUsernameQuery(
                "select user_id, pw, active from members where user_id=?");

        // define query to retrieve the quthorities/roles by username
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
                "select user_id, roles from role where user_id=?");

        return jdbcUserDetailsManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
        http.authorizeHttpRequests(cofigurer ->
                cofigurer
                        .requestMatchers(HttpMethod.GET,"/api/employees").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET,"/api/employees/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST,"/api/employees").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH,"/api/employees/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT,"/api/employees").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE,"/api/employees/**").hasRole("ADMIN")

);

        // use HTTP Basic authenticatiıon
        http.httpBasic(Customizer.withDefaults());

        // disable Cross Site Request Forgery (CSRF)
        // in general , not required for stateless REST APIs that use POST, PUT, DELETE and/or PATCH
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }


     /*
    @Bean
    public InMemoryUserDetailsManager userDetailsManager(){

        UserDetails erdogan = User.builder()
                .username("erdogan")
                .password("{noop}test123")
                .roles("EMPLOYEE")
                .build();

        UserDetails irem = User.builder()
                .username("irem")
                .password("{noop}test123")
                .roles("EMPLOYEE","MANAGER")
                .build();

        UserDetails gamze = User.builder()
                .username("gamze")
                .password("{noop}test123")
                .roles("EMPLOYEE","MANAGER","ADMIN")
                .build();
        return new InMemoryUserDetailsManager(erdogan, irem, gamze);

    }
*/

}

/*
    burası Spring Security konfigürasyonu. İki ana işi var:
	1.	Kimlik doğrulama (authentication): Kullanıcıların nereden ve nasıl doğrulanacağını ayarlıyor.
	2.	Yetkilendirme (authorization): Hangi endpoint’e hangi rolün erişebileceğini kural olarak yazıyor.

    1) Sınıf ve Bean’ler
    @Configuration
    public class DemoSecurityConfig { ... }
    	•	@Configuration: Bu sınıf bir konfigürasyon sınıfı; içindeki @Bean metotları
    	    Spring konteynerine bileşen olarak eklenir.


    a) Kullanıcı Yönetimi – JDBC tabanlı
    @Bean
public UserDetailsManager userDetailsManager(DataSource dataSource){
    JdbcUserDetailsManager jdbc = new JdbcUserDetailsManager(dataSource);

    jdbc.setUsersByUsernameQuery(
        "select user_id, pw, active from members where user_id=?");

    jdbc.setAuthoritiesByUsernameQuery(
        "select user_id, roles from role where user_id=?");

    return jdbc;
}

	•	Amaç: Kullanıcıları ve rollerini veritabanından okumak (hard-coded ya da in-memory değil).
	•	DataSource: Spring, application.properties/yaml’daki DB bilgilerine göre sağlar.
	•	JdbcUserDetailsManager:
	•	setUsersByUsernameQuery(...): Kullanıcıyı getirir. Sorgunun 3 sütunu şu sırayla dönmesi
	  beklenir: username, password, enabled(active).
	•	Senin sorgunda bunlar user_id, pw, active.
	•	setAuthoritiesByUsernameQuery(...): Kullanıcının yetkilerini (rollerini) getirir.
	    Dönüşte 2 sütun beklenir: username, authority.
	•	Senin sorgunda user_id, roles. (Buradaki sütun adı “authority/role” olması önemli değil;
	    sıra ve veri tipi uygun olsun yeter.)
	•	Önemli nokta (ROLE_ prefix): hasRole("EMPLOYEE") kullanıyorsan veritabanındaki yetki
	    metninin ROLE_EMPLOYEE olarak saklanması gerekir. (Spring hasRole çağrısında otomatik
	    ROLE_ prefix ekler.)

    Not: Spring’in varsayılan şeması users(username, password, enabled) ve
    authorities(username, authority) tablolarıdır. Sen özel tablo/sütun adı vererek
    bunu override etmişsin.



b) HTTP güvenlik zinciri (filter chain)

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(configurer ->
        configurer
            .requestMatchers(HttpMethod.GET,"/api/employees").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.GET,"/api/employees/**").hasRole("EMPLOYEE")
            .requestMatchers(HttpMethod.POST,"/api/employees").hasRole("MANAGER")
            .requestMatchers(HttpMethod.PATCH,"/api/employees/**").hasRole("MANAGER")
            .requestMatchers(HttpMethod.PUT,"/api/employees").hasRole("MANAGER")
            .requestMatchers(HttpMethod.DELETE,"/api/employees/**").hasRole("ADMIN")
    );

    // HTTP Basic Authentication
    http.httpBasic(Customizer.withDefaults());

    // CSRF kapalı (stateless REST için tipik)
    http.csrf(csrf -> csrf.disable());

    return http.build();
}

	•	Yetkilendirme Kuralları:
	•	GET /api/employees ve GET /api/employees/** → EMPLOYEE rolü
	•	POST /api/employees ve PUT /api/employees ve PATCH /api/employees/** → MANAGER rolü
	•	DELETE /api/employees/** → ADMIN rolü
	•	httpBasic: İstek geldiğinde Basic Auth (Authorization header) ile
       	kullanıcı adı/şifre ister. Yanlışsa 401 döner.

	•	CSRF disable: REST API’lerde (özellikle session tutmayan/stateless, form submit olmayan)
	CSRF çoğunlukla kapatılır.


        Kısaca Akış
	•	İstemci GET /api/employees çağırır → Basic Auth ile kullanıcı adı/şifre gönderir → Spring Security DB’den kullanıcıyı & rollerini JdbcUserDetailsManager ile çeker → kural hasRole("EMPLOYEE") sağlanırsa Controller’a geçer,
	    aksi halde 403 Forbidden / 401 Unauthorized döner.



	    Dikkat Edilecek Noktalar & İyileştirme Önerileri
	1.	ROL formatı
	•	DB’de roller ROLE_EMPLOYEE, ROLE_MANAGER, ROLE_ADMIN olarak saklanmalı.
	•	Alternatif: hasAuthority("EMPLOYEE") kullanırsan prefix zorunluluğu kalkar (ama best practice ROLE_ kullanmaktır).
	2.	Şema isimleri
	•	Sorguların döndürdüğü sütun sırası doğru: (username, password, enabled) ve (username, authority).
	•	Tabloların isimleri custom (members, role). Sorun yok; sadece sorguların çalıştığından emin ol.
	3.	Parola şifreleme
	•	DB’de parolayı BCrypt ile sakla.
	•	PasswordEncoder tanımlayıp DaoAuthenticationProvider veya JdbcUserDetailsManager ile kullan:

        @Bean
    PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    }

    Özet
	•	JdbcUserDetailsManager: Kimlik bilgisi ve rolleri veritabanından okur (custom SQL ile).
	•	SecurityFilterChain: Hangi HTTP metodunun/endpoint’in hangi ROL ile erişilebilir olduğunu belirler.
	•	httpBasic + csrf().disable(): REST API için uygun, basit kimlik doğrulama ve CSRF kapatma.
	•	Roller DB’de ROLE_ prefix’iyle saklanmalıdır (hasRole kullanıldığı için).



 */



