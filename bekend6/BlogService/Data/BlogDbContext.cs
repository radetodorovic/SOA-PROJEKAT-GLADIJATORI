using Microsoft.EntityFrameworkCore;
using BlogService.Models;

namespace BlogService.Data;

public class BlogDbContext(DbContextOptions<BlogDbContext> options) : DbContext(options)
{
    public DbSet<Blog> Blogs => Set<Blog>();
    public DbSet<BlogImage> BlogImages => Set<BlogImage>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Blog>(entity =>
        {
            entity.HasKey(blog => blog.Id);

            entity.Property(blog => blog.AuthorId)
                .IsRequired();

            entity.Property(blog => blog.Title)
                .HasMaxLength(200)
                .IsRequired();

            entity.Property(blog => blog.Description)
                .HasColumnType("text")
                .IsRequired();

            entity.Property(blog => blog.CreatedAtUtc)
                .IsRequired();

            entity.HasMany(blog => blog.Images)
                .WithOne(image => image.Blog)
                .HasForeignKey(image => image.BlogId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<BlogImage>(entity =>
        {
            entity.HasKey(image => image.Id);

            entity.Property(image => image.Url)
                .HasColumnType("text")
                .IsRequired();

            entity.Property(image => image.OrderIndex)
                .IsRequired();
        });
    }
}
