package com.trainee.reciptai.Repository;

import com.trainee.reciptai.model.ShoppingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemsRepo extends JpaRepository<ShoppingItemEntity,Long> {

    Optional<ShoppingItemEntity> findByItemNameContainingIgnoreCase(String itemName);

}
