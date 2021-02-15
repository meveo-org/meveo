# Development installation

The easiest way is to use [vscode with devcontainer](https://code.visualstudio.com/docs/remote/containers)
you only need to have docker and vscode installed on your machine (along with WSL integrated to both docker and vscode if you are on win10).

in you linux env (possibly WSL), clone this repo and open vscode:
```
cd /home
git clone git@github.com:meveo-org/meveo.git
cd meveo
code .
```

This will open vscode and since the repo contain a `.devcontainer/devcontainer.json` vscode will propose you to be reopened in a container.

by default it will build the image completely but if you are working on the master branch (or a branch you created from it) then you can speedup
the build by changing in `.devcontainer/docker-compose.yml` the following lines
```
  meveo:
      #image: manaty/meveo-dev:latest 
      build: 
        context: ../
        dockerfile: Dockerfile.dev
      container_name: meveo
```
by
```
  meveo:
      image: manaty/meveo-dev:latest 
      #build: 
      #  context: ../
      #  dockerfile: Dockerfile.dev
      container_name: meveo
```

once vscode reopens, you can access meveo on `http://localhost:8080/meveo` (default credential meveo.admin/meveo)

any html,or jsf file is automatically deployed when saved
to hot deploy java code execute the java deggubber by going to the `Run` menu (`Ctrl+Shift+D`) then `Launch debugging` or `Launch Current File button` (F5)

you can monitor the logs from your machine :
```
docker logs meveo -f
```
