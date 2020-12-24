package com.spring

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Student (
    //@Id @GeneratedValue var id: Long,
    @Id var index: String,
    var name: String,
    var surname: String
)