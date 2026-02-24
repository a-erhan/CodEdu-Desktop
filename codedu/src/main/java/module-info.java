module com.codedu {
    // --- Mevcut JavaFX Gereksinimleri ---
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // --- YENİ: Veritabanı ve JPA (Hibernate) Gereksinimleri ---
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.instrument;

    // --- YENİ: Spring Boot Gereksinimleri ---
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires org.aspectj.weaver;
    requires static lombok;
    requires atlantafx.base;

    // --- JavaFX FXML İzinleri ---
    opens com.codedu.views to javafx.fxml;
    opens com.codedu.controllers to javafx.fxml, spring.core, spring.beans, spring.context;
    // --- YENİ: Hibernate ve Spring'in Model (Entity) sınıflarımıza erişip tablo oluşturabilmesi için izinler ---
    opens com.codedu.models to org.hibernate.orm.core, spring.core, spring.beans;
    opens com.codedu to spring.core, spring.beans, spring.context;

    // --- Dışarı Aktarımlar ---
    exports com.codedu;
    exports com.codedu.models;
    exports com.codedu.controllers;
}