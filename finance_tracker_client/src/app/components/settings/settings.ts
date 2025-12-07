import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';
import { SidebarComponent } from '../shared/sidebar';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './settings.html',
  styleUrls: ['./settings.css']
})
export class SettingsComponent {
  selectedFile: File | null = null;
  message = '';
  error = '';

  constructor(
    private userService: UserService, 
    private authService: AuthService,
    private router: Router
  ) {}

  onFileSelected(event: any) {
      if (event.target.files && event.target.files[0]) {
          this.selectedFile = event.target.files[0];
      }
  }

  updateAvatar() {
    if (!this.selectedFile) {
        this.error = 'Please select a file first.';
        return;
    }
    
    this.userService.updateAvatar(this.selectedFile).subscribe({
      next: (res) => {
          this.message = 'Avatar updated successfully';
          this.error = '';
          this.selectedFile = null;
          
          // Delay reload so user can read message
          setTimeout(() => {
             location.reload(); 
          }, 1500);
      },
      error: (err) => {
          this.error = 'Failed to update avatar';
          this.message = '';
      }
    });
  }

  removeAvatar() {
    if (confirm('Are you sure you want to remove your avatar?')) {
        this.userService.deleteAvatar().subscribe({
            next: (res) => {
                this.message = 'Avatar removed successfully';
                this.error = '';
                // Delay reload so user can read message
                setTimeout(() => {
                    location.reload(); 
                }, 1500);
            },
            error: (err) => {
                this.error = 'Failed to remove avatar';
                this.message = '';
            }
        });
    }
  }

  deleteAccount() {
    if(confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
        this.userService.deleteAccount().subscribe({
            next: () => {
                alert('Account deleted.');
                this.authService.logout();
                this.router.navigate(['/login']);
            },
            error: (err) => this.error = 'Failed to delete account'
        });
    }
  }
}
