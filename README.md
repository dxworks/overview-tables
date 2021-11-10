# Dx Overview tables
A command line utility to transform csv files created by dx to Excel file.

## Installation & Usage
Install either as a standalone npm executable or as a dxw cli plugin:

#### NPM executable:

```shell
npm i -g @dxworks/tables

dx-tables [path/to/dx/folder]
```

#### dxw cli plugin
Requires   `dxw` to be installed. (You can install it running `npm i -g @dxworks/cli)
```shell
dxw plugin i @dxworks/tables

dxw tables [path/to/dx/folder]
```

## Results
Produces a folder called `presentation` where it adds the overview tables excel and ppt.