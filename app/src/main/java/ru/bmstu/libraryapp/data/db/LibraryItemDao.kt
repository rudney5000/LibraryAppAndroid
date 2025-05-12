package ru.bmstu.libraryapp.data.db

import androidx.room.*
import ru.bmstu.libraryapp.data.db.entities.LibraryItemEntity

@Dao
interface LibraryItemDao {
    @Query("""
        SELECT * FROM library_items 
        ORDER BY 
            CASE WHEN :sortBy = 'title' THEN title 
                 ELSE createdAt 
            END 
        LIMIT :limit 
        OFFSET :offset
    """)
    suspend fun getItems(sortBy: String, limit: Int, offset: Int): List<LibraryItemEntity>

    @Query("DELETE FROM library_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Int): Int

    @Query("SELECT COUNT(*) FROM library_items")
    suspend fun getItemCount(): Int

    @Insert
    suspend fun insert(item: LibraryItemEntity): Long

    @Update
    suspend fun update(item: LibraryItemEntity)

    @Delete
    suspend fun delete(item: LibraryItemEntity)
}