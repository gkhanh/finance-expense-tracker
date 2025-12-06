import { TestBed } from '@angular/core/testing';
import { DataService } from './data';

describe('DataService', () => {
  let service: DataService | undefined;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
