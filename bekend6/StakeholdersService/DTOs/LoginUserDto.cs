using System.ComponentModel.DataAnnotations;

namespace StakeholdersService.DTOs;

public class LoginUserDto
{
    [Required]
    public string Identifier { get; set; } = string.Empty;

    [Required]
    public string Password { get; set; } = string.Empty;
}
