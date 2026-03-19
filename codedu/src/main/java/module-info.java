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
    requires spring.tx;

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
    exports com.codedu.models.matchmaking;
    opens com.codedu.models.matchmaking to org.hibernate.orm.core, spring.beans, spring.core;
    exports com.codedu.models.social;
    opens com.codedu.models.social to org.hibernate.orm.core, spring.beans, spring.core;
    exports com.codedu.models.learning;
    opens com.codedu.models.learning to org.hibernate.orm.core, spring.beans, spring.core;
    exports com.codedu.models.gamification;
    opens com.codedu.models.gamification to org.hibernate.orm.core, spring.beans, spring.core;
    exports com.codedu.models.user;
    opens com.codedu.models.user to org.hibernate.orm.core, spring.beans, spring.core;
}