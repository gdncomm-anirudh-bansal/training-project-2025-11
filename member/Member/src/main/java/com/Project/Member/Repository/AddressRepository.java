package com.Project.Member.Repository;

import com.Project.Member.Entity.Address;
import com.Project.Member.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address,String> {
}
