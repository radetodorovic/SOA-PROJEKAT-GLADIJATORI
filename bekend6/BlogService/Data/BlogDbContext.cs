using Microsoft.EntityFrameworkCore;
using BlogService.Models;

namespace BlogService.Data;

public class BlogDbContext(DbContextOptions<BlogDbContext> options) : DbContext(options)
{
    public DbSet<Blog> Blogs => Set<Blog>();
    public DbSet<BlogImage> BlogImages => Set<BlogImage>();
    public DbSet<BlogComment> BlogComments => Set<BlogComment>();
    public DbSet<BlogLike> BlogLikes => Set<BlogLike>();

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

        modelBuilder.Entity<BlogComment>(entity =>
        {
            entity.HasKey(comment => comment.Id);

            entity.Property(comment => comment.BlogId)
                .IsRequired();

            entity.Property(comment => comment.UserId)
                .IsRequired();

            entity.Property(comment => comment.Text)
                .HasColumnType("text")
                .IsRequired();

            entity.Property(comment => comment.CreatedAtUtc)
                .IsRequired();

            entity.Property(comment => comment.UpdatedAtUtc)
                .IsRequired();

            entity.HasOne(comment => comment.Blog)
                .WithMany(blog => blog.Comments)
                .HasForeignKey(comment => comment.BlogId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<BlogLike>(entity =>
        {
            entity.HasKey(like => like.Id);

            entity.Property(like => like.BlogId)
                .IsRequired();

            entity.Property(like => like.UserId)
                .IsRequired();

            entity.Property(like => like.CreatedAtUtc)
                .IsRequired();

            entity.HasOne(like => like.Blog)
                .WithMany(blog => blog.Likes)
                .HasForeignKey(like => like.BlogId)
                .OnDelete(DeleteBehavior.Cascade);

            // Sprje?ava da isti korisnik lajkuje istu objavu više puta
            entity.HasIndex(like => new { like.BlogId, like.UserId })
                .IsUnique();
        });
    }
}
