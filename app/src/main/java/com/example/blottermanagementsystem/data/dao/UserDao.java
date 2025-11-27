package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.User;
import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAllUsers();
    
    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(int userId);
    
    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);
    
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);
    
    @Query("UPDATE users SET resetCode = :code, resetCodeExpiry = :expiry WHERE email = :email")
    void setResetCode(String email, String code, long expiry);
    
    @Query("UPDATE users SET password = :newPassword, resetCode = NULL, resetCodeExpiry = 0 WHERE email = :email")
    void resetPassword(String email, String newPassword);
    
    @Query("SELECT * FROM users WHERE role = :role")
    List<User> getUsersByRole(String role);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);
    
    @Update
    void updateUser(User user);
    
    @Delete
    void deleteUser(User user);
    
    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    int getUserCountByRole(String role);
    
    @Query("SELECT COUNT(*) FROM users WHERE role NOT IN ('Admin', 'Officer') AND isActive = 1")
    int getTotalUserCount();
    
    @Query("UPDATE users SET fcmToken = :token, deviceId = :deviceId WHERE id = :userId")
    void updateFcmToken(int userId, String token, String deviceId);
}
