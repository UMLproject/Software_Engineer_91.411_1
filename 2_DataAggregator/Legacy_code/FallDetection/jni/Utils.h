/*
 * Copyright (c) 2010 Jordan Frank, HumanSense Project, McGill University
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 * See LICENSE for more information
 *
 * Utils.h
 */

#include "ANNx.h"
#include <cxcore.h>
#ifndef ulong
#define ulong unsigned long
#endif
#ifndef uint
#define uint unsigned int
#endif

#ifndef UTILS_H_
#define UTILS_H_

typedef struct {
	long length;
	long exclude;
	int verbosity;
	int delay;
	int indim;
	int embdim;
	int pcaembdim;
	char *column;
	char *infile;
	char *outfile;
	char stdo;
} Settings;

void get_embedding(Settings* settings, ANNcoord*& data, int &length);
void convert_to_ann_points(ANNpointArray &dataPts, ANNcoord* series, int rows, int cols);
void get_ann_points(ANNpointArray &dataPts, ANNcoord* series, long rows, int cols);
void print_matrix(CvMat* matrix, FILE *fout);
void check_alloc(void *pointer);
double **get_multi_series(char *name, long *l, long ex,
		int *col, char *which, char colfix, int verbosity);
char* getline(char *str, int *size, FILE *fin, int verbosity);

#define HS_TAG "HUMANSENSE"
#define MAT_TYPE CV_32FC1
#define FLOAT_SCAN "%G"
#define FLOAT_OUT "%.8G"
#define CHECK_ALLOC_NOT_ENOUGH_MEMORY 12
#define GET_MULTI_SERIES_WRONG_TYPE_OF_C 21
#define GET_MULTI_SERIES_NO_LINES 22

// Defines the buffer size for reading lines.
#define INPUT_SIZE 1024

/* The possible names of the verbosity levels */
#define VER_INPUT 0x1
#define VER_USR1 0x2
#define VER_USR2 0x4
#define VER_USR3 0x8
#define VER_USR4 0x10
#define VER_USR5 0x20
#define VER_USR6 0x40
#define VER_FIRST_LINE 0x80


#endif /* UTILS_H_ */
