using Microsoft.EntityFrameworkCore;
using StakeholdersService.Models;

namespace StakeholdersService.Data;

public class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<User> Users => Set<User>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(user => user.Id);
            entity.HasIndex(user => user.Username).IsUnique();
            entity.HasIndex(user => user.Email).IsUnique();

            entity.Property(user => user.Username)
                .HasMaxLength(50)
                .IsRequired();

            entity.Property(user => user.Email)
                .HasMaxLength(100)
                .IsRequired();

            entity.Property(user => user.PasswordHash)
                .HasMaxLength(255)
                .IsRequired();

            entity.Property(user => user.Role)
                .HasConversion<string>()
                .HasMaxLength(20)
                .IsRequired();

            entity.Property(user => user.IsBlocked)
                .HasDefaultValue(false)
                .IsRequired();

            entity.Property(user => user.FirstName)
                .HasMaxLength(100);

            entity.Property(user => user.LastName)
                .HasMaxLength(100);

            entity.Property(user => user.ProfileImage)
                .HasColumnType("text");

            entity.Property(user => user.Biography)
                .HasColumnType("text");

            entity.Property(user => user.Motto)
                .HasMaxLength(500);

            entity.Property(user => user.IsProfileInitialized)
                .HasDefaultValue(false)
                .IsRequired();
        });
    }
}
