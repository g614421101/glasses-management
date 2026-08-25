package com.glasses.dto;

import lombok.Data;

@Data
public class ImportResultDTO {
    private int sysUserInserted;
    private int sysUserSkipped;
    private int customerInserted;
    private int customerSkipped;
    private int optometryInserted;
    private int optometrySkipped;
    private int salesInserted;
    private int salesSkipped;
    private String mode; // "merge" or "replace"
}
