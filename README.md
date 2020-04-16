# Sick apple trees 🌿🍎

Predict from what disease your apple tree suffers!

This repository contains code for Android application 📲 that predict apple tree disease based on picture of its leaf.

# Dataset
I've used Plant Pathology 2020 Kaggle comptetition data. 
This dataset consist of 1821 training images. There are four classes present: rust, scab, multiple diseases	and healthy.

For visualizations check this notebook: https://github.com/sfczekalski/plant_pathology/blob/master/notebooks/EDA.ipynb

# Neural network 🧠
I've chosen ResNet18 architecture, that was pretrained on ImageNet dataset.

Notebook with model training can be found here: https://github.com/sfczekalski/plant_pathology/blob/master/notebooks/Training.ipynb

The model scored 0.957 ROC AUC on unseen test set.

Hopefully it turns out to be usefull for someone 👩🏼‍🌾.
