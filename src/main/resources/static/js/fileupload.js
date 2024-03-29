const KB = 1024;
const MB = 1024*1024;
const MAX_FILE_SIZE = 500 * MB;

function getSize(size) {
    if (size < KB) {
        return `${size}B`;
    }
    if (size < MB) {
        return `${(size / KB).toFixed(1)}KB`
    }
    return `${(size / MB).toFixed(1)}MB`
}

function removeFile(fileInput, fileIndex) {
    const inputFiles = Array.from(fileInput.files);
    const newFileList = new DataTransfer();
    inputFiles.splice(fileIndex, 1);
    inputFiles.forEach(item => newFileList.items.add(item));
    fileInput.files = newFileList.files;
    fileInput.dispatchEvent(new Event("change"));
}

let filesElements = document.getElementsByName("files");

function setHiddenAttribute(fileList, hidden) {
    if (hidden) {
        fileList.setAttribute('hidden', true);
    } else {
        fileList.removeAttribute('hidden');
    }
}

function createListItem() {
    const li = document.createElement('li');
    li.className = 'list-group-item align-middle';
    return li;
}

function createRemoveButton(onclick) {
    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.className = 'btn-close';
    removeButton.onclick = onclick;
    return removeButton;
}

function createFileName(file) {
    const filename = document.createElement('span');
    if ('webkitRelativePath' in file && file.webkitRelativePath.length > 0) {
        filename.textContent = file.webkitRelativePath;
    } else {
        filename.textContent = file.name;
    }
    return filename;
}

function createFileSize(file) {
    const filesize = document.createElement('span');
    filesize.textContent = ' ' + getSize(file.size);
    filesize.className = 'fst-italic';
    return filesize;
}

function createHeaderText(totalSize) {
    const totalSizeSpan = document.createElement('span');
    totalSizeSpan.textContent = `Total size: ${getSize(totalSize)}/${getSize(MAX_FILE_SIZE)}`;
    return totalSizeSpan;
}

function setHeaderClasses(header, disabled) {
    header.className = 'list-group-item ';
    if (disabled) {
        header.className += 'list-group-item-danger';
    } else {
        header.className += 'list-group-item-primary';
    }
}

function setSubmitButtonClasses(submitButton, disabled) {
    if (disabled) {
        submitButton.className = 'btn btn-primary w-100 disabled';
    } else {
        submitButton.className = 'btn btn-primary w-100';
    }
}

filesElements.forEach(fileInput => {
    const fileForm = fileInput.parentElement;
    const fileList = fileInput.nextElementSibling;
    const submitButton = fileForm.querySelector('button[type="submit"]');

    fileInput.addEventListener('change', function() {
        setHiddenAttribute(fileList, this.files.length <= 0);

        let totalSize = 0;
        fileList.innerHTML = '<li></li>'; // Only header element inside

        for (let i = 0; i < this.files.length; i ++) {
            const file = this.files[i];
            totalSize += file.size;

            const li = createListItem();
            fileList.appendChild(li);

            li.appendChild(createRemoveButton(() => removeFile(fileInput, i)));
            li.appendChild(createFileName(file));
            li.appendChild(createFileSize(file));
        }

        const header = fileList.querySelector('li');
        setHeaderClasses(header, totalSize > MAX_FILE_SIZE);
        header.innerHTML = '';
        header.appendChild(createHeaderText(totalSize));

        setSubmitButtonClasses(submitButton, totalSize > MAX_FILE_SIZE);
    });
});
