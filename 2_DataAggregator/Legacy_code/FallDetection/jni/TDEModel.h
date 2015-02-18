/*
 * Copyright (c) 2010 Jordan Frank, HumanSense Project, McGill University
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 * See LICENSE for more information
 *
 * TDEModel.h
 */

#include <cxcore.h>
#include "Utils.h"
#include "ANNx.h"
//#include <kd_dump.cpp>
//#include <kd_dump.cpp>
#ifndef TDEMODEL_H_
#define TDEMODEL_H_

class TDEModel {
public:
	TDEModel(Settings* settings);
	TDEModel(FILE *model_file);
	virtual ~TDEModel();

	void DumpTree(char* outfile);
	void getKNN(ANNpoint ap, int k, ANNidxArray nn_idx, ANNdistArray dists);
    void simulateTrajectory(ANNpoint s0, ANNpointArray trajectory, int dim, long  N);
    ANNpoint getDataPoint(int idx);
    ANNcoord *projectData(ANNcoord *data, int rows, int cols);

    int getLength() const { return length; }
    int getEmbDim() const { return embdim; }
    int getDelay() const { return delay; }
    bool getUsePCA() const { return use_pca; }
    int getPCAEmbDim() const {
    	if (use_pca) {
    		return bases->cols;
    	}
    	else {
    		return embdim;
    	}
    }
private:
    int length, embdim, delay;
    ANNpointArray dataPts;
    DLL_API ANNkd_tree *kdTree;
    // Related to the PCA
    void computePCABases(ANNcoord *data, int rows,int cols, int numbases);
    bool use_pca;
    CvMat* avg;
    CvMat* bases;
};

#endif /* TDEMODEL_H_ */
