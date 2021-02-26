# jenkins-pipelines-v1
"""

kubectl create sa k8-tools -n jenkins
kubectl create clusterrolebinding k8-tools-bind --clusterrole=cluster-admin --serviceaccount=jenkins:k8-tools


kubectl get sa -n jenkins
kubectl get clusterrolebinding k8-tools-bind  -o yaml

"""