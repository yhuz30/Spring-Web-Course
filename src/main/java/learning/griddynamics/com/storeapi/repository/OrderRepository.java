package learning.griddynamics.com.storeapi.repository;

import learning.griddynamics.com.storeapi.entity.Order;
import learning.griddynamics.com.storeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);
}