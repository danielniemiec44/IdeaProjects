package com.spring

import org.springframework.data.repository.CrudRepository

interface StudentRepositories : CrudRepository<Student, Long>{
    fun findByIndex(index: String) : Student
}