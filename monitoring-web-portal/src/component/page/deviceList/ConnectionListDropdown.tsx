import React, {useEffect, useState} from 'react';

const ConnectionListDropdown: React.FC = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [connectionList, setConnectionList] = useState<string[]>([]);
    const [selectedConnection, setSelectedConnection] = useState<string>('');

    useEffect(() => {
        fetch('http://localhost:8080/websocket/connections')
            .then(res => res.json())
            .then(data => setConnectionList(data))
            .catch(() => setConnectionList([]))
    }, [])

    const handleSelect = (connection: string) => {
        setSelectedConnection(connection);
        setIsOpen(false);
    };

    return (
        <div className="connection-dropdown">
            <button
                className="dropdown-trigger"
                onClick={() => setIsOpen(!isOpen)}
            >
                {selectedConnection || 'Select Connection'}
                <span className={`arrow ${isOpen ? 'up' : 'down'}`}>▼</span>
            </button>

            {isOpen && (
                <div className="dropdown-menu">
                    {connectionList.map((connection, index) => (
                        <div
                            key={index}
                            className="dropdown-item"
                            onClick={() => handleSelect(connection)}
                        >
                            <div className="connection-icon">🔗</div>
                            <div className="connection-info">
                                <div className="connection-name">{connection}</div>
                                <div className="connection-status">Active</div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ConnectionListDropdown;
