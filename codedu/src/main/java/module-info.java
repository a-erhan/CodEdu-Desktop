module com.codedu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.instrument;

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
    requires jakarta.transaction;
    requires spring.web;

    opens com.codedu.views to javafx.fxml;
    opens com.codedu.controllers to javafx.fxml, spring.core, spring.beans, spring.context;
    opens com.codedu.models to org.hibernate.orm.core, spring.core, spring.beans;
    opens com.codedu to spring.core, spring.beans, spring.context;
    opens com.codedu.repositories.interfaces to spring.core, spring.beans, spring.context;
    opens com.codedu.repositories.implementations to spring.core, spring.beans, spring.context, org.hibernate.orm.core;
    opens com.codedu.services to spring.core, spring.beans, spring.context;

    exports com.codedu;
    exports com.codedu.models;
    exports com.codedu.controllers;
    exports com.codedu.repositories.interfaces;
    exports com.codedu.services;
}